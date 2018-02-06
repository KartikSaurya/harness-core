package software.wings.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import software.wings.beans.NameValuePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UtilTest {
  @Test
  public void testToProperties() {
    List<NameValuePair> nameValuePairList = new ArrayList<>();
    nameValuePairList.add(NameValuePair.builder().name("n1").value("v1").build());
    nameValuePairList.add(NameValuePair.builder().name("n2").value("v2").build());
    nameValuePairList.add(NameValuePair.builder().name("n3").value(null).build());

    Map map = Util.toProperties(nameValuePairList);
    assertNotNull(map);
    assertEquals(3, map.size());
  }
}
