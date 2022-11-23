package server.gateway;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import server.gateway.saman.PaymentIFBindingLocator;
import server.lib.interfacing.BankInterface;
import server.lib.model.Request;
import server.lib.orm.dao.Dao;
import server.lib.utils.Controller;
import server.lib.utils.FileUtil;
import server.lib.utils.Rest;
import server.model.Payment;

import java.io.File;
import java.util.UUID;

public class Saman implements BankInterface {

//    java -cp axis.jar;commons-logging-1.0.4.jar;commons-discovery-0.2.jar;jaxrpc.jar;saaj.jar;wsdl4j-1.5.1.jar org.apache.axis.wsdl.WSDL2Java https://verify.sep.ir/Payments/ReferencePayment.asmx?wsdl

    private static final String URL = "https://sep.shaparak.ir/MobilePG/MobilePayment";
    private static final String GATEWAY = "https://sep.shaparak.ir/OnlinePG/OnlinePG";
    private static final String TERMINAL = "50088009";

    @Override
    public Payment init(Payment payment, String callBackUrl) throws Throwable {
        JsonObject object = new JsonObject();
        object.addProperty("Action", "token");
        object.addProperty("TerminalId", TERMINAL);
        object.addProperty("RedirectUrl", callBackUrl);
        object.addProperty("ResNum", payment.getPayment_code());
        object.addProperty("Amount", payment.getPayment_amount().intValue() * 10);
        object.addProperty("CellNumber", payment.getPayment_identity());
        String data = Rest.sync(URL, HttpMethod.POST, object);
        String token = null;
        String status = null;
        try {
            JsonObject jsonObject = JsonParser.parseString(data).getAsJsonObject();
            status = jsonObject.get("status").getAsString();
            token = jsonObject.get("token").getAsString();
        } catch (Throwable ignored) {
        }
        payment.setPayment_token(token);
        payment.setPayment_result_code(status);
        if (token != null && status != null && status.equalsIgnoreCase("1")) {
            payment.setPayment_url(callBackUrl.replace("payment/verify?", "payment/redirect?"));
        }
        return payment;
    }

    @Override
    public ResponseEntity redirect(Payment payment) throws Throwable {
        File file = FileUtil.getTemplate("payment/form.html");
        Document document = Jsoup.parse(file, "UTF-8");
        document.getElementById("Token").val(payment.getPayment_token());
        document.getElementById("GetMethod").val("true");
        return Controller.passHTMLDOM(document);
    }

    @Override
    public Payment verify(Request request, Dao<Payment, UUID> dao) throws Throwable {
        Payment stock = null;
        try {
            for (String key : request.getParameterMap().keySet()) {
                System.out.println("Key: " + key + "   Value: " + request.getParameter(key));
            }
            String state = request.getParameter("State");
            String rrn = request.getParameter("RRN");
            String mid = request.getParameter("MID");
            String refNum = request.getParameter("RefNum");
            String resNum = request.getParameter("ResNum");
            String terminalId = request.getParameter("TerminalId");
            String traceNo = request.getParameter("TraceNo");
            String amount = request.getParameter("Amount");
            String card = request.getParameter("SecurePan");
            String id = request.getParameter("id");
            if (state != null && id != null) {
                stock = dao.queryBuilder().where().eq(Payment.CODE, resNum).and().eq(Payment.ID, UUID.fromString(id)).queryForFirst();
                if (stock != null && stock.getPayment_state() == Payment.STATE_INIT && (Integer.parseInt(amount) / 10) == stock.getPayment_amount()) {
                    if (state.equalsIgnoreCase("OK")) {
                        long count = dao.queryBuilder().where().eq(Payment.REF, refNum).countOf();
                        if (count == 0) {
                            PaymentIFBindingLocator locator = new PaymentIFBindingLocator();
                            double data = locator.getPaymentIFBindingSoap().verifyTransaction(refNum, TERMINAL);
                            System.out.println("Verified; " + data + "  " + refNum + "  " + mid + "  " + terminalId);
                            stock.setPayment_terminal(terminalId);
                            stock.setPayment_card(card);
                            stock.setPayment_ref_code(refNum);
                            stock.setPayment_merchant(mid);
                            stock.setPayment_trace(traceNo);
                            stock.setPayment_result_code(rrn);
                            stock.setPayment_state(Payment.STATE_DONE);
                        }
                    }
                    if (stock.getPayment_state() == Payment.STATE_INIT) {
                        stock.setPayment_state(Payment.STATE_CANCEL);
                    }
                    dao.update(stock);
                } else {
                    return null;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return stock;
    }
}
