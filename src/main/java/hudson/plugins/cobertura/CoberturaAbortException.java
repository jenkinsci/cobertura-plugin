package hudson.plugins.cobertura;

import hudson.AbortException;

/**
 * Common abort exception that should be thrown to abort the build. Prepends
 * a common prefix to the abort message.
 */
public class CoberturaAbortException extends AbortException {
  /**
   * CoberturaAbortException constructor
   * @param  String message       the abort message
   * @return        CoberturaAbortException 
   */
    public CoberturaAbortException(String message) {
        super("[Cobertura] " + message);
    }

}
