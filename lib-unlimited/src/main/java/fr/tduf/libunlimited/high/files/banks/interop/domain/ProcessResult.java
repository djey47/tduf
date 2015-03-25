package fr.tduf.libunlimited.high.files.banks.interop.domain;

/**
 * Object to be returned after a process exits normally. Bring all details about execution result.
 */
public class ProcessResult {
    private int returnCode = -1;

    private String out = "";

    private String err = "";

    /**
     * @param returnCode    : value of exit status code from process
     * @param out           : contents of standard output
     * @param err           : contents of error output
     */
    public ProcessResult(int returnCode, String out, String err) {
        this.returnCode = returnCode;
        this.out = out;
        this.err = err;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public String getOut() {
        return out;
    }

    public String getErr() {
        return err;
    }
}