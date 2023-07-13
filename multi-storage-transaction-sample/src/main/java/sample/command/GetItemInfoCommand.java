package sample.command;

import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import sample.Sample;

@Command(name = "GetItemInfo", description = "Get item information")
public class GetItemInfoCommand implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "ITEM_NAME", description = "Item name")
    private String itemName;

    @Override
    public Integer call() throws Exception {
        try (Sample sample = new Sample()) {
            System.out.println(sample.getItemInfo(itemName));
        }
        return 0;
    }
}
