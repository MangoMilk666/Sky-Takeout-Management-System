package runner;

public class GenerateOrderRunner {

    public static void main(String[] args) {
        String[] forwarded = new String[args.length + 1];
        forwarded[0] = "orders";
        System.arraycopy(args, 0, forwarded, 1, args.length);
        DataGeneratorMain.main(forwarded);
    }
}
