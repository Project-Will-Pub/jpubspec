package xyz.rk0cc.willpub.exceptions.pubspec;

import java.io.Serializable;

/**
 * A given interface of {@link Exception} which related with pubspec operation.
 *
 * @since 1.0.0
 */
public interface PubspecException extends Serializable {
    /**
     * Additional information that causing this exception throw.
     * <br/>
     * It usually appends the last line in {@link Exception#toString()} for giving quick diagnose dor debug or report
     * issue.
     *
     * @return Summarize information that causing this {@link Exception} thrown.
     */
    String getCausedConfigurationMessage();
}
