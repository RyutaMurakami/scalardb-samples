package sample.command;

import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import sample.Sample;

@Command(name = "AddItemCommand", description = "Add a new item")
public class AddItemCommand implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "ITEM_ID", description = "Item ID")
    private int itemId;

    @Parameters(index = "1", paramLabel = "NAME", description = "Item name")
    private String itemName;

    @Parameters(index = "2", paramLabel = "PRICE", description = "Item price")
    private int price;

    @Parameters(index = "3", paramLabel = "SHOP_ID", description = "Shop ID")
    private int shopId;

    @Parameters(index = "4", paramLabel = "STOCK", description = "Item stock")
    private int stock;

    @Override
    public Integer call() throws Exception {
        try (Sample sample = new Sample()) {
            sample.addNewItem(itemId, itemName, price, shopId, stock);
        }
        return 0;
    }
}
