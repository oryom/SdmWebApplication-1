package utils;


import SDM_CLASS.IfYouBuy;
import SDM_CLASS.SDMDiscount;
import SDM_CLASS.SDMOffer;
import SDM_CLASS.ThenYouGet;
import constants.Constants;
import logic.Logic.Engine;
import logic.Logic.My_CLASS.*;
import logic.users.UserManager;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServletUtils {

	//private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";
	private static final String CHAT_MANAGER_ATTRIBUTE_NAME = "chatManager";// need change to my logic

	/*
	Note how the synchronization is done only on the question and\or creation of the relevant managers and once they exists -
	the actual fetch of them is remained un-synchronized for performance POV
	 */
	private static final Object userManagerLock = new Object();
	private static final Object engineLock = new Object();
	private static final Object chatManagerLock = new Object();


	public static UserManager getUserManager(ServletContext servletContext) {

		synchronized (userManagerLock) {
			if (servletContext.getAttribute(Constants.USER_MANAGER_ATTRIBUTE_NAME) == null) {
				servletContext.setAttribute(Constants.USER_MANAGER_ATTRIBUTE_NAME, new UserManager());
			}
		}
		return (UserManager) servletContext.getAttribute(Constants.USER_MANAGER_ATTRIBUTE_NAME);
	}

	public static Engine getEngine(ServletContext servletContext) {

		synchronized (engineLock) {
			if (servletContext.getAttribute(Constants.ENGINE_ATTRIBUTE_NAME) == null) {
				servletContext.setAttribute(Constants.ENGINE_ATTRIBUTE_NAME, new Engine());
			}
		}
		return (Engine) servletContext.getAttribute(Constants.ENGINE_ATTRIBUTE_NAME);
	}

	public static void showOffersToUser(PrintWriter out, Map<MyStoreItem, Double> selectedItemsMap, MySuperMarket superMarket) {
		out.println("<form id ='selectSpecialOffers' method='POST' action='/createStaticOrder'>" +
		//out.println("<form id ='selectSpecialOffers' method='POST' action=''>" +
				"<div class='row'>" +
				"<h3>Special offers</h3>" +
				"</div>");

		Set<MyStoreItem> storeItemSet =selectedItemsMap.keySet() ;
		for (MyStoreItem storeItem:storeItemSet) {
			MyStore store = superMarket.getStores().getStoreMap().get(storeItem.getStoreId());
			if(store.getSdmStore() !=null) {
				if (store.getSdmStore().getSDMDiscounts() != null) { // if the store has discounters.
					for (SDMDiscount discount : store.getSdmStore().getSDMDiscounts().getSDMDiscount()) {
						IfYouBuy ifYouBuy = discount.getIfYouBuy();
						if (storeItem.getMyItem().getSdmItem().getId() == ifYouBuy.getItemId() &&
								selectedItemsMap.get(storeItem) >= ifYouBuy.getQuantity())
							for (double i = 0;
								 i < Math.floor(selectedItemsMap.get(storeItem) / ifYouBuy.getQuantity()); i++) {
								out.println(createSellTile(discount, superMarket, store)); // need to build single offer html
							}
					}
				}
			}
		}

		out.println("<div class=\"row\">");
		out.println("<input type=\"submit\" value='Next' >");
		out.println("</div>");
		out.println("</form>");

	}


	private static String createSellTile(SDMDiscount discount, MySuperMarket superMarket, MyStore store) {
		String res = "";
		ThenYouGet thenYouGet = discount.getThenYouGet();
		if(thenYouGet.getOperator().equals("ONE-OF"))
			res = oneOfHtml(discount, superMarket,store);
		else
			res = takeAllOrNothingHtml(discount, superMarket,store);

		return res;
	}

	private static String takeAllOrNothingHtml(SDMDiscount discount, MySuperMarket superMarket, MyStore store) {
		ThenYouGet thenYouGet = discount.getThenYouGet();
		String ifYouBuyItemName = superMarket.getItems().getItemsMap().
				get(discount.getIfYouBuy().getItemId()).getName();
		double ifYouBuyQuantityString = discount.getIfYouBuy().getQuantity();
		String allItemNames = "";
		Double itemTotalPrice = 0.0;
		int index = 0;
		String res = "<div class='row'>" +
				"<div class=\"discount\" name='discount'>" +
				"<input type = \"checkbox\" name='discountCheckBox' value='ALL-NOTHING' class=\"regular-checkbox\">" +
				"<label class='discount-header' >"+discount.getName()+ " Provided by: " + store.getName()+"</label >"+
				"<label > for buying "+ ifYouBuyQuantityString +" " + ifYouBuyItemName+" "+"</label >";
		for (SDMOffer offer:thenYouGet.getSDMOffer()) {
			String itemName = superMarket.getItems().getItemsMap().get(offer.getItemId()).getName();
			allItemNames = allItemNames + " " + offer.getQuantity() + " " + itemName +
					" for " + offer.getForAdditional() + " NIS ;";
			itemTotalPrice = itemTotalPrice +offer.getForAdditional();
			allItemNames = allItemNames + "\n";
			res += "<input type=\"hidden\" id='"+index+"' name=\"offerItemId\" value='"+offer.getItemId()+"'>" +
					"<input type=\"hidden\" id='"+index+"' name=\"offerItemPrice\" value='"+offer.getForAdditional()+"'>" +
					"<input type=\"hidden\" id='"+index+"' name=\"offerItemQuantity\" value='"+offer.getQuantity()+"'>" +
					"<input type=\"hidden\" id='"+index+"' name=\"offerItemStoreId\" value='"+store.getId()+"'>";
			index++;
		}
		allItemNames = allItemNames + "Total of " + itemTotalPrice + "NIS ";
		res+="<label >you can choose all or nothing: </label >" +
				"<label >"+ allItemNames +"</label >" +
				"</div>" +
				"</div>";

		return res;
	}

	//need have:::::::::    itemId,price,storeId

	private static String oneOfHtml(SDMDiscount discount, MySuperMarket superMarket, MyStore store) {
		ThenYouGet thenYouGet = discount.getThenYouGet();
		String ifYouBuyItemName = superMarket.getItems().getItemsMap().
				get(discount.getIfYouBuy().getItemId()).getName();
		double ifYouBuyQuantityString = discount.getIfYouBuy().getQuantity();
		String res =  "<div class='row'>" +
				"<div class=\"discount\" name='discount'>" +
				"<input type = \"checkbox\" name='discountCheckBox' value='ONE-OF' class=\"regular-checkbox\">" +
				"<label class='discount-header' >"+discount.getName() +" Provided by: " + store.getName() +"</label >" +
				"<label >for buying "+ ifYouBuyQuantityString +" " + ifYouBuyItemName  +"</label >" +
				"<label >you can choose one of:</label >" +
				"<select name=\"products\" id=\"oneOfOfferSelect\" class='discount-one-of'>" ;
		for (SDMOffer offer: thenYouGet.getSDMOffer()) {
			String itemName = superMarket.getItems().getItemsMap().get(offer.getItemId()).getName();
			double itemQuantity = offer.getQuantity();
			int forAddition = offer.getForAdditional();
			res += "<option value=\"volvo\" offerItemId='"+offer.getItemId()+"' " +
					"offerItemPrice='"+offer.getForAdditional()+"' offerItemStoreId='"+store.getId()+"' " +
					"offerItemQuantity='"+offer.getQuantity()+"'>" +
					itemQuantity + " of "+itemName + " for " +forAddition +
					"NIS for each</option>";

		}
		res += "<input type=\"hidden\" id=\"selectedOfferItemId\" name=\"selectedOfferItemId\" value=''>" +
				"<input type=\"hidden\" id=\"selectedOfferItemPrice\" name=\"selectedOfferItemPrice\" value=''>" +
				"<input type=\"hidden\" id=\"selectedOfferItemQuantity\" name=\"selectedOfferItemQuantity\" value=''>" +
				"<input type=\"hidden\" id=\"selectedOfferItemStoreId\" name=\"selectedOfferItemStoreId\" value=''>";
		res+=  "</select>" +
				"</div>" +
				"</div>";
		return res;
	}


	public static Date buildDate(String date) throws ParseException {
		Date date1=new SimpleDateFormat("YYYY-MM-dd").parse(date);
		return date1;
	}

	public static MyCustomer updateCustomerLocatin(Engine engine, HttpServletRequest req, int customerX, int customerY) {
		String customerName = SessionUtils.getUsername(req);
		MyCustomer customer = engine.getMyUsers().findCustomerByName(customerName);
		MyLocation customerLocation = new MyLocation(customerX,customerY);
		customer.setLocation(customerLocation);
		return customer;
	}

	public static Map<Integer, Double> createDeliveryCostMap(Engine engine, HttpServletRequest req
			, MyCustomer customer, Map<MyStoreItem, Double> quantityMap) {

		Map<Integer, Double> deliveryCostMap = new HashMap<>();
		String areaName = SessionUtils.getAreaName(req);
		MySuperMarket superMarket = engine.getMySupermarkets().getAreaSuperMarketByName(areaName);
		superMarket.calculateDeliveryCostMap(quantityMap,deliveryCostMap,customer);

		return deliveryCostMap;
	}

	public static Map<Integer, MyStoreSingleOrderItems> createSingleOrderItemsMap(MyOrder order
			, HttpServletRequest req, Engine engine) {
		Map<Integer, MyStoreSingleOrderItems> storeSingleOrderItemsMap = new HashMap<>();
		String areaName = SessionUtils.getAreaName(req);
		MySuperMarket superMarket = engine.getMySupermarkets().getAreaSuperMarketByName(areaName);

		engine.createStoreSingleOrderInstance(order,superMarket,storeSingleOrderItemsMap);
		return storeSingleOrderItemsMap;
	}

	public static String buildHtmlFormForOrrderSummery(MyOrder order, Map<Integer
			, MyStoreSingleOrderItems> storeSingleOrderItemsMap, Engine engine, HttpServletRequest req) {

		String areaName = SessionUtils.getAreaName(req);
		MySuperMarket superMarket = engine.getMySupermarkets().getAreaSuperMarketByName(areaName);
		String res = "";

		res+="<div id=\"content\">" +
				"<div class='row'>" +
				"<div class ='col'>" +
				"<div class='row'>" +
				"<h3>Order summery</h3>" +
				"</div>";
		for(Integer storeId : storeSingleOrderItemsMap.keySet()){
			MyStore store = superMarket.getStores().getStoreMap().get(storeId);
			res+= "<div class='store'>" +
					"<div class=\"row\">" +
						"<div class=\"col-25\">" +
						"<label for=\"fname\">Store Id</label>" +
						"</div>" +
						"<div class=\"col-75\">" +
						"<label id='storeNameLabel' > "+store.getId() +"</label >" +
						"</div>" +
					"</div>" +
					"<div class=\"row\">" +
						"<div class=\"col-25\">" +
						"<label for=\"fname\">Store Name</label>" +
						"</div>" +
						"<div class=\"col-75\">" +
						"<label id='storeNameLabel' > "+store.getName() +"</label >" +
						"</div>" +
					"</div>" +
					"<div class=\"row\">" +
						"<div class=\"col-25\">" +
						"<label for=\"fname\">Store PPK</label>" +
						"</div>" +
						"<div class=\"col-75\">" +
						"<label >"+store.getPPK()+"</label >" +
						"</div>" +
					"</div>" +

					"<div class=\"row\">" +
						"<div class=\"col-25\">" +
						"<label for=\"fname\">Distance from customer</label>" +
						"</div>" +
						"<div class=\"col-75\">" +
						"<label >"+String.format("%.2f",storeSingleOrderItemsMap.get(storeId).getDistanceFromCustomer())+"</label >" +
						"</div>" +
					"</div>" +

					"<div class=\"row\">" +
						"<div class=\"col-25\">" +
						"<label for=\"fname\">Delivery Cost</label>" +
						"</div>" +
						"<div class=\"col-75\">" +
						"<label >"+String.format("%.2f",storeSingleOrderItemsMap.get(storeId).getDeliveryCost())+"</label >" +
						"</div>" +
					"</div>" +

					"<div id=\"orderItems\">" +
						"<div class=\"row\">" +
						"<h4>Order Items:</h4>" +
						"</div>" +
						"<table id=\"orderItemsTable\">" +
							"<tr>" +
							"<th>Item Id</th>" +
							"<th>Item Name</th>" +
							"<th>Purchase Category</th>" +
							"<th>Quantity</th>" +
							"<th>Single item price</th>" +
							"<th>Total Price</th>" +
							"<th>Store/Offer</th>" +
							"</tr>";

			for(MyStoreItem storeItem :
					storeSingleOrderItemsMap.get(storeId).getThisStoreQuantityMapFromOrder().keySet()){
				double quantity = storeSingleOrderItemsMap.get(storeId)
						.getThisStoreQuantityMapFromOrder().get(storeItem);

				res+="<tr>" +
						"<td>"+storeItem.getMyItem().getItemId()+"</td>" +
						"<td>"+storeItem.getName()+"</td>" +
						"<td>"+storeItem.getMyItem().getPurchaseCategory()+"</td>" +
						"<td>"+quantity+"</td>" +
						"<td>"+storeItem.getPrice()+"</td>" +
						"<td>"+String.format("%.2f",quantity * storeItem.getPrice())+"</td>" +
						"<td>"+storeItem.getItemKind()+"</td>" +
						"</tr>";

			}
			res+= " </table>" +
					" </div>" +
					" </div>";
					//"<div class=\"row\">" +
					//"<form id=\"sendOrder\" method=\"POST\" action=\"openFeedbackOption\">" +
					//"<input id=\"accept\" type='button' onclick='return acceptOrderButton()' name=\"\" value='Approve order'/>" +
					//"<input id=\"decline\" type='button' onclick='return declineOrderButton()' name=\"\" value='Abort'/>" +
					//"</form>" +
					//"</div>";

		}

		res+= 	"<div class=\"row\">" +
					"<form id=\"sendOrder\" method=\"POST\" action=\"\">" +
					//"<form id=\"sendOrder\" method=\"POST\" action=\"openFeedbackOption\">" +
					"<input id=\"accept\" type='button' onclick='return acceptOrderButton()' value='Approve order'/>" +
					"<input id=\"decline\" type='button' onclick='return declineOrderButton()'value='Abort'/>" +
					"</form>" +
				"</div>"+
					"<div id='feedbacksForm' class='leaveFeedbacks'>" +
					"</div>" +
					"</div>" +
					"</div>" +
					"</div>";


		return res;
	}
}
