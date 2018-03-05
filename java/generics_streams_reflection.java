  @ServiceAction
  public ProductFilters buildFilters(GetProductResponse productResponse) {
    ProductFilters filters = new ProductFilters();    
    List<Callable<Void>> taskList = new ArrayList<>();    
        
    taskList.add(execute(() -> filters.brandFilter = buildFilter(productResponse.products.getProducts(), Product::getMarque)));    
    taskList.add(execute(() -> filters.characteristicFilter = buildFilter(productResponse.products.getProducts(), Product::getCaracteristique)));
    taskList.add(execute(() -> filters.typeFilter = buildFilter(productResponse.products.getProducts(), Product::getTypeProduit)));
    taskList.add(execute(() -> filters.paymentFilter = buildFilter(productResponse.products.getProducts(), Product::getVersementSugg)));
    taskList.add(execute(() -> filters.priceFilter = buildPriceFilter(productResponse.products.getProducts(), Product::getPrixAffichageBigDecimal)));    
    
    try
    {
      Executors.newFixedThreadPool(taskList.size()).invokeAll(taskList);
    }
    catch (InterruptedException ie)
    {
       logger.error(ie.getMessage());
    }
    
    return filters;
  }

  protected static Callable<Void> execute(Runnable action) {
    return () -> { action.run(); return null; };
  }

  private static <K> Map<K, Long> buildFilter(List<Product> products, Function<Product,? extends K> classifier) {
    return products.parallelStream()        
        .collect(Collectors.groupingBy(classifier, Collectors.counting()));
  }

  private static <T> List<T> filter(Collection<? extends T> source, Predicate<? super T> predicate) {
    final List<T> result = new ArrayList<>(source.size());
    
    for (T element: source)
      if (predicate.apply(element))
        result.add(element);
    
    return result;
  }

  private static Map<String, Object> objectProperties(Object object) {
    try {
      Map<String, Object> map = new HashMap<>();
      
      Arrays.asList(Introspector.getBeanInfo(object.getClass(), Object.class).getPropertyDescriptors())
            .stream()
            // filter out properties with setters only
            .filter(pd -> Objects.nonNull(pd.getReadMethod()))
            .forEach(pd -> { // invoke method to get value
                try {
                    Object value = pd.getReadMethod().invoke(object);
                    
                    if (value != null) {
                        map.put(pd.getName(), value);
                    }
                } catch (Exception e) {
                    // add proper error handling here
                }
            });
      return map;
    } catch (IntrospectionException e) {   
      return Collections.emptyMap();
    }
  }
