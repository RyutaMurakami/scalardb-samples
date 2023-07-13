package sample.command;

import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import sample.Sample;

@Command(name = "IncreaseItemStock", description = "Increase item stock")
public class IncreaseItemStockCommand implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "ITEM_ID", description = "Item ID")
    private int itemId;

    @Parameters(index = "1", paramLabel = "AMOUNT", description = "Amount to increase")
    private int amount;

    @Override
    public Integer call() throws Exception {
        try (Sample sample = new Sample()) {
            sample.increaseItemStock(itemId, amount);
        }
        return 0;
    }
}
