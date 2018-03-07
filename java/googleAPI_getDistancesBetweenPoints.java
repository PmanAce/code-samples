@Service
public class GoogleMapsService extends BaseService {
  private final GeoApiContext apiContext;

  /*
   * https://developers.google.com/maps/documentation/distance-matrix/
   */
  @Autowired
  public GoogleMapsService(final @NotNull YAMLConfig config) {
    super();
    
    apiContext = new GeoApiContext.Builder()
        .apiKey(config.getGoogleMapsAPIKey())
        .maxRetries(config.getGoogleMapsRetries())
        .retryTimeout(config.getGoogleMapsRetryTimeoutMs(), TimeUnit.MILLISECONDS)
        .build();    
  }
  
  @Cacheable(value = "StoreDistances")
  public List<Long> getDistance(String origin, String[] destinations) {
    List<Long> distances = new ArrayList<>();        
    
    Lists.partition(Arrays.asList(destinations), 25).forEach(
        partition -> {                          
            try {              
              Arrays.asList(launchRequest(origin, partition).rows).stream()
                .forEach(distanceMatrixRow ->                 
                  distances.addAll(Arrays.asList(distanceMatrixRow.elements).stream().map(
                      element -> 
                        element.distance.inMeters).collect(Collectors.toList()))
                );
            } catch (ApiException | InterruptedException | IOException e) {                
              logger.error(e.getMessage());
            }                        
        });      
      
      return distances;    
  }

  private DistanceMatrix launchRequest(String origin, List<String> partition)
      throws ApiException, InterruptedException, IOException {
        return DistanceMatrixApi.newRequest(apiContext)    
            .origins(origin)
            .destinations(partition.toArray(new String[0]))
            .language("en")
            .mode(TravelMode.DRIVING)
            .await();    
  }  
}
