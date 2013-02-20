package ro.isdc.wro.model.resource.processor.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.util.WroUtil;


/**
 * Encapsulates the matcher creation for css backround url's detection. Useful to isolate unit tests.
 *
 * @author Alex Objelean
 * @created 20 Feb 2013
 * @since 1.6.3
 */
public class CssUrlInspector {
  private static final Logger LOG = LoggerFactory.getLogger(CssUrlInspector.class);
  private static final Pattern PATTERN = Pattern.compile(WroUtil.loadRegexpWithKey("cssUrlRewrite"));

  public String findAndReplace(final String content, final ItemHandler handler) {
    final Matcher matcher = getMatcher(content);
    final StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      // Do not process @import statements
      LOG.debug("Matched group: {}", matcher.group());
      //TODO use CssImportInspector
      if (matcher.group().matches("@import\\b.*")) {
        continue;
      }
      LOG.debug("No @import detected");
      final String originalDeclaration = getOriginalDeclaration(matcher);
      final String originalUrl = getOriginalUrl(matcher);
      LOG.debug("originalUrl: {}", originalUrl);

      Validate.notNull(originalUrl);
      matcher.appendReplacement(sb, handler.replace(originalDeclaration, originalUrl));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  /**
   * @return the {@link Matcher} for processed css content.
   */
  protected Matcher getMatcher(final String content) {
    return PATTERN.matcher(content);
  }

  protected String getOriginalDeclaration(final Matcher matcher) {
    return matcher.group(0);
  }

  protected String getOriginalUrl(final Matcher matcher) {
    /**
     * index of the group containing an url inside a declaration of this form:
     *
     * <pre>
     * body {
     *   filter: progid:DXImageTransform.Microsoft.AlphaImageLoader(src='../images/tabs/tabContent.png', sizingMethod='scale' );
     * }
     * </pre>
     *
     * or
     *
     * <pre>
     * @font-face {
     *   src: url(btn_icons.png);
     * }
     * </pre>
     */
    final String groupA = matcher.group(1);

    /**
     * index of the group containing an url inside a declaration of this form:
     *
     * <pre>
     * body {
     *     background: #B3B3B3 url(img.gif);
     *     color:red;
     * }
     * </pre>
     */
    final String originalUrl = groupA != null ? groupA : matcher.group(2);
    return originalUrl;
  }

  public static interface ItemHandler {
    String replace(String originalDeclaration, String originalUrl);
  }
}
