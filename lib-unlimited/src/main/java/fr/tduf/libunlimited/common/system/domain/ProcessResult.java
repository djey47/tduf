package fr.tduf.libunlimited.common.system.domain;

import com.esotericsoftware.minlog.Log;

/**
 * Object to be returned after a process exits normally. Bring all details about execution result.
 */
public class ProcessResult {
    private static final String THIS_CLASS_NAME = ProcessResult.class.getSimpleName();

    private int returnCode = -1;

    private String out = "";

    private String err = "";

    private String commandName;

    /**
     * @param commandName   : command to execute, without its arguments
     * @param returnCode    : value of exit status code from process
     * @param out           : contents of standard output
     * @param err           : contents of error output
     */
    public ProcessResult(String commandName, int returnCode, String out, String err) {
        this.commandName = commandName;
        this.returnCode = returnCode;
        this.out = out;
        this.err = err;
    }

    /**
     * Displays process return code and standard output to {@link System}.out.
     * Terminates with a new line.
     */
    public void printOut() {
        Log.info(THIS_CLASS_NAME, "Process finished with return code: " + returnCode);
        System.out.println(this.out);
    }

    /**
     * Displays process return code and error output to {@link System}.err.
     * Terminates with a new line.
     */
    public void printErr() {
        System.err.println(this.err);
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

    public String getCommandName() {
        return commandName;
    }
}