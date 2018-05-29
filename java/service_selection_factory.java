@Service
@RequiredArgsConstructor
public class ShippingCostsServiceFactory extends BaseService {

  private final List<ShippingCostsService> serviceList;  
  
  public ShippingCostsService get() {
    
    return serviceList
            .stream()
            .filter(service -> service.supports())
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
  }
}




@Service
@RequiredArgsConstructor
public class EconomaxShippingCostsService extends BaseService implements ShippingCostsService {

  private final UserService userService;
  
  @Override  
  public Object determineShippingCosts() {
    
    return "EconomaxShippingCostsService";
  }

  @Override
  public boolean supports() {

    User user = userService.getCurrentUser();
    
    return user.getBanniere().equalsIgnoreCase("ECONOMAX");
  }
}





@Service
@RequiredArgsConstructor
public class BMShippingCostsService extends BaseService implements ShippingCostsService {

  private final UserService userService;
  
  @Override
  public Object determineShippingCosts() {
    
    return "BMShippingCostsService";
  }

  @Override
  public boolean supports() {

    User user = userService.getCurrentUser();
    
    return user.getBanniere().equalsIgnoreCase("BM");
  }
}
