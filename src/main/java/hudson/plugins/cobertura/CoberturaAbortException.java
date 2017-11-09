package hudson.plugins.cobertura;

import hudson.AbortException;

/**
 *
 * @author jeffpearce
 */
public class CoberturaAbortException extends AbortException {
    public CoberturaAbortException(String message) {
        super("[Cobertura] " + message);
    }
    
}
