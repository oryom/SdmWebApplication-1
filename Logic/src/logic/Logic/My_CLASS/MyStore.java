package logic.Logic.My_CLASS;


import SDM_CLASS.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyStore {
    private SDMStore sdmStore;
    private MyStoreItems storeItems;
    private Map<Integer,MyOrder> storeOrderMap;
    private MyLocation myLocation;
    private List<MyStoreSingleOrderItems> storeSingleOrderItemsList;
    private  String ownerName ;
    private double totalOrdersCost = 0 ;
    private double totalDeliveryCosts = 0 ;

    private String name;
    private int PPK;
    private int id;

    /*
    ownerName
    totalOrder costs
    total deliveryCosts
    * */

    public MyStore(SDMStore sdmStore, MyItems items, String ownerName) {
        this.id = sdmStore.getId();
        this.name = sdmStore.getName();
        this.PPK = sdmStore.getDeliveryPpk();
        this.sdmStore = sdmStore;
        this.storeOrderMap = new HashMap<>();
        this.ownerName = ownerName;
        this.storeItems = new MyStoreItems(sdmStore,items);
        this.myLocation = new MyLocation(this.getSdmStore().getLocation());
        this.storeSingleOrderItemsList = new ArrayList<>();
    }

    public MyStore(int id, StoreJson store, List<MyItem> storeItemsToAdd, String ownerName) {
        this.id = id;
        this.name = store.name;
        this.PPK = store.ppk;
        this.ownerName = ownerName;
        this.storeOrderMap = new HashMap<>();

        this.storeItems = new MyStoreItems();
        createStoreItems(store.items, storeItemsToAdd);

        this.myLocation = new MyLocation(store.x, store.y);
        this.storeSingleOrderItemsList = new ArrayList<>();

    }

    private void createStoreItems(List<MyItem.ItemJson> itemsJson, List<MyItem> storeItemsToAdd){
        MyStoreItem newStoreItem;
        int index = 0;

        for(MyItem item : storeItemsToAdd){
            // MyStoreItem.itemKind == "store" ???
            newStoreItem = new MyStoreItem(item, itemsJson.get(index++).price, this.id, "store");
            this.storeItems.addStoreItem(newStoreItem);
        }
    }

    public SDMStore getSdmStore() {
        return sdmStore;
    }

    public void setSdmStore(SDMStore sdmStore) {
        this.sdmStore = sdmStore;
    }

    public MyStoreItems getStoreItems() {
        return storeItems;
    }

    public void setStoreItems(MyStoreItems storeItems) {
        this.storeItems = storeItems;
    }

    public Map<Integer, MyOrder> getStoreOrderMap() {
        return storeOrderMap;
    }

    public void setStoreOrderMap(Map<Integer, MyOrder> storeOrderMap) {
        this.storeOrderMap = storeOrderMap;
    }

    public MyLocation getMyLocation() {
        return myLocation;
    }

    public void setMyLocation(MyLocation myLocation) {
        this.myLocation = myLocation;
    }


    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }

    public int getPPK() { return PPK; }

    public String getOwnerName() {
        return ownerName;
    }

    public double getTotalOrdersCost() {
        return totalOrdersCost;
    }

    public double getTotalDeliveryCosts() {
        return totalDeliveryCosts;
    }

    @Override
    public String toString() {
        return "Store id: " + getId() +
                 " ,Store name: " + getName() +
                " ,Location:" + getMyLocation();
    }

    public List<MyStoreSingleOrderItems> getStoreSingleOrderItemsList() {
        return storeSingleOrderItemsList;
    }

    public void setStoreSingleOrderItemsList(List<MyStoreSingleOrderItems> storeSingleOrderItemsList) {
        this.storeSingleOrderItemsList = storeSingleOrderItemsList;
    }

    public double calculateTotalDelivryEarn() {
        double retValue = 0 ;
        for (MyStoreSingleOrderItems items: this.getStoreSingleOrderItemsList()) {
            retValue = retValue + items.getDeliveryCost();
        }
        return retValue;
    }



    public ObservableList<StoreItemView> createObservableListForItemsView() {
        ObservableList<StoreItemView> observableList = FXCollections.observableArrayList();
        for (MyStoreItem storeItem:this.getStoreItems().getItemsList()) {
            observableList.add(new StoreItemView(storeItem));
        }

        return observableList;
    }

    public ObservableList<StoreOrdersView> createObservableListForOrdersView() {
        ObservableList<StoreOrdersView> observableList = FXCollections.observableArrayList();
        for (MyStoreSingleOrderItems singleOrderItems:this.getStoreSingleOrderItemsList() ) {
            observableList.add(new StoreOrdersView(singleOrderItems));
        }
        return observableList;
    }

    public ObservableList<StoreOffersView> createObservableListForOffersView() {
        ObservableList<StoreOffersView> observableList = FXCollections.observableArrayList();
        if(this.getSdmStore().getSDMDiscounts() != null) {
            for (SDMDiscount discount : this.getSdmStore().getSDMDiscounts().getSDMDiscount()) {
                observableList.add(new StoreOffersView(discount, this));
            }
        }
        return observableList;
    }

    public void deleteItemFromStore(MyStoreItem storeItem) {
        storeItems.getItemsList().remove(storeItem);
        storeItems.getItemsMap().remove(storeItem.getMyItem().getItemId(),storeItem);
        deleteOfferThatContainsThisItem(storeItem);
        }

    private void deleteOfferThatContainsThisItem(MyStoreItem storeItem) {
        int itemId  = storeItem.getMyItem().getItemId();
        List<SDMDiscount> discountList = this.getSdmStore().getSDMDiscounts().getSDMDiscount();
        List<SDMDiscount> discontToremove = new ArrayList<>();
        for (SDMDiscount discount:discountList) {
            if(discount.getIfYouBuy().getItemId() == itemId)
                discontToremove.add(discount);
            else {
                for (SDMOffer offer:discount.getThenYouGet().getSDMOffer()) {
                    if(offer.getItemId()==itemId)
                        discontToremove.add(discount);
                }
            }
        }
        if(!discontToremove.isEmpty())
            for (SDMDiscount discount:discontToremove) {
                discountList.remove(discount);
            }

    }

    public boolean addItemToStore(MyItem item,int price) {
        if(storeItems.getItemsMap().containsKey(item.getItemId()))
            return false;
        else{
            MyStoreItem addingItem= new MyStoreItem(item,price,this.getId(),"store");
            this.storeItems.getItemsMap().put(item.getItemId(),addingItem);
            this.storeItems.getItemsList().add(addingItem);
            return true;
        }
    }

    public void updatePrice(MyStoreItem storeItem, int newPrice) {
        MyStoreItem itemToUpdate =  this.storeItems.getItemsMap().get(storeItem.getMyItem().getItemId());
        itemToUpdate.setPrice(newPrice);
    }


    public class StoreJson{
        public String name;
        public int x;
        public int y;
        public int ppk;
        public List<MyItem.ItemJson> items;

        public StoreJson(String name, int x, int y, int ppk, List<MyItem.ItemJson> items){
            this.name = name;
            this.x = x;
            this.y = y;
            this.ppk = ppk;
            this.items = items;
        }

        @Override
        public String toString() {
            return "Store{" +
                    "name='" + name + '\'' +
                    ", x=" + x +
                    ", y=" + y +
                    ", ppk=" + ppk +
                    ", items=" + items +
                    '}';
        }
    }
}
