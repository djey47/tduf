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

    /**
     * Displays process return code and standard output to {@link System}.out.
     * Terminates with a new line.
     */
    public void printOut() {
        printReturnCode();
        System.out.println(this.out);
    }

    /**
     * Displays process return code and error output to {@link System}.err.
     * Terminates with a new line.
     */
    public void printErr() {
        printReturnCode();
        System.err.println(this.err);
    }

    private void printReturnCode() {
        System.out.println("Process finished with return code: " + returnCode);
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