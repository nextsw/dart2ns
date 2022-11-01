package store;

public class CustomFieldService {
  private static CustomFieldService INS = new CustomFieldService();

  private CustomFieldService() {}

  public static CustomFieldService get() {
    return INS;
  }

  public ICustomFieldProcessor<?> getProcessor(String type) {
    return null;
  }
}
