package sample.command;

import java.util.concurrent.Callable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import sample.Sample;

@Command(name = "GetItemsByShopId", description = "Get items by shop ID")
public class GetItemsByShopIdCommand implements Callable<Integer> {

    @Parameters(index = "0", paramLabel = "SHOP_ID", description = "Shop ID")
    private int shopId;

    @Override
    public Integer call() throws Exception {
        try (Sample sample = new Sample()) {
            System.out.println(sample.getItemsByShopId(shopId));
        }
        return 0;
    }
}
