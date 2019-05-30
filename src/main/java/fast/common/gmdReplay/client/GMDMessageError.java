package fast.common.gmdReplay.client;

import java.util.ArrayList;

public class GMDMessageError {
    public static class ErrorDetails implements Comparable<ErrorDetails> {
        private final String label_path;
        private final String expected_value;
        private final String actual_value;

        public String getLabel_path() {
            return label_path;
        }
        public ErrorType getType() {
            return type;
        }

        private final ErrorType type;

        ErrorDetails(String label_path, String expected_value, String actual_value, ErrorType type) {
            this.label_path = label_path;
            this.expected_value = expected_value;
            this.actual_value = actual_value;
            this.type = type;
        }

        public String getErrorText() {
            StringBuilder sb =  new StringBuilder();
            switch (type) {
                case Value:
                    sb.append("Wrong value at '" + label_path + "', expected value: "
                            + expected_value + ", actual value: " + actual_value);
                    break;
                case Missing:
                    sb.append("Missing value at '" + label_path + "', expected value: " + expected_value);
                    break;
                case Unexpected:
                    sb.append("Unexpected value at '" + label_path + "', actual value: " + actual_value);
                    break;
                default:
                    //Should never come here
            }
            return sb.toString();
        }

        @Override
        public int compareTo(ErrorDetails o) {
            return this.label_path.compareTo(o.label_path);
        }
        
        @Override
		public boolean equals(Object o) {
			return this.label_path.equals(((ErrorDetails)o).label_path);
      }

    }

    private final String symbol;
    private final String msg_type;
    private final ArrayList<ErrorDetails> errors;

    public ArrayList<ErrorDetails> getErrors() {
        return errors;
    }

    public GMDMessageError(String symbol, String msg_type) {
        this.symbol = symbol;
        this.msg_type = msg_type;
        this.errors = new ArrayList<>();
    }

    public String getSymbol() {
        return symbol;
    }

    public String getMsg_type() {
        return msg_type;
    }

    public String addValueError(String label_path, String expected_value, String actual_value){
        ErrorDetails err = new ErrorDetails(label_path, expected_value, actual_value, ErrorType.Value);
        errors.add(err);
        return err.getErrorText();
    }

    public String addMissingElement(String label_path, String expected_value){
        ErrorDetails err = new ErrorDetails(label_path, expected_value, null, ErrorType.Missing);
        errors.add(err);
        return err.getErrorText();
    }

    public String addUnexpectedElement(String label_path, String actual_value){
        ErrorDetails err = new ErrorDetails(label_path, null, actual_value, ErrorType.Unexpected);
        errors.add(err);
        return err.getErrorText();
    }

    public int getErrNumber() {
        return errors.size();
    }

    public String getErrorDescription() {
        StringBuilder sb = new StringBuilder("There are ");
        sb.append(errors.size());
        sb.append(" discrepancies in the '");
        sb.append(msg_type);
        sb.append("' message for symbol '");
        sb.append(symbol);
        sb.append("':\n");
        for(ErrorDetails err : errors) {
            sb.append(err.getErrorText());
            sb.append("\n");
        }
        return sb.toString();
    }

    public void clear() {
        errors.clear();
    }
}
