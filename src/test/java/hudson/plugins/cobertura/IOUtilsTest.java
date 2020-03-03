package hudson.plugins.cobertura;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IOUtilsTest {

  @Test
  public void testSanitizeRelativePathInUnix() {
    String fileName = "../aaa/bbb";
    String sanitizedName = IOUtils.sanitizeFilename(fileName);

    assertEquals("_2e_2e_2faaa_2fbbb", sanitizedName);

    fileName = "aaa/../bbb";
    sanitizedName = IOUtils.sanitizeFilename(fileName);

    assertEquals("aaa_2f_2e_2e_2fbbb", sanitizedName);
  }

  @Test
  public void testSanitizeRelativePathInWindows() {
    String fileName = "..\\aaa\\bbb";
    String sanitizedName = IOUtils.sanitizeFilename(fileName);

    assertEquals("_2e_2e_5caaa_5cbbb", sanitizedName);

    fileName = "aaa\\..\\bbb";
    sanitizedName = IOUtils.sanitizeFilename(fileName);
    assertEquals( "aaa_5c_2e_2e_5cbbb", sanitizedName);
  }


  @Test
  public void testSanitizeAbsolutePathInUnix() {
    String fileName = "/aaa/bbb";
    String sanitizedFileName = IOUtils.sanitizeFilename(fileName);

    assertEquals("_2faaa_2fbbb", sanitizedFileName);
  }

  @Test
  public void testSanitizeAbsolutePathInWindows() {
    String fileName = "C:\\aaa\\bbb";
    String sanitizedName = IOUtils.sanitizeFilename(fileName);

    assertEquals("C_3a_5caaa_5cbbb", sanitizedName);
  }
}
