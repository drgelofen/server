package server.lib.interfacing;

import org.springframework.http.ResponseEntity;
import server.lib.model.Request;
import server.lib.orm.dao.Dao;
import server.model.Payment;

import java.util.UUID;

public interface BankInterface {

    Payment init(Payment payment, String callBackUrl) throws Throwable;

    ResponseEntity redirect(Payment payment) throws Throwable;

    Payment verify(Request request, Dao<Payment, UUID> dao) throws Throwable;
}
