package com.saba.taxpro;

import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import in.amitv.mongowork.dto.TaxRecDTO;
import in.amitv.mongowork.service.MongoHelperUtil;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.FaultOutInterceptor;
import org.apache.cxf.message.Message;

public class TaxFaultOutInterceptor extends FaultOutInterceptor {

	@Override
	public void handleMessage(Message message) throws Fault {
		super.handleMessage(message);
		TaxRecDTO dto = (TaxRecDTO) message.getExchange().getInMessage().remove("in.amitv.mongowork.dto.taxrec");
		dto.getResp().setTs(new Date());
        Integer responseCode = (Integer)message.get(Message.RESPONSE_CODE);
        if (responseCode != null) {
        	dto.getResp().setResponseCode(responseCode.toString());
        }else{
        	dto.getResp().setResponseCode("500");
        }
        String ct = (String)message.get(Message.CONTENT_TYPE);
        if (ct != null) {
        	dto.getResp().setContentType(ct);
        }
        Object headers = message.get(Message.PROTOCOL_HEADERS);
        if (headers != null) {
        	dto.getResp().setHeaders(headers.toString());
        }

		MongoHelperUtil.addTaxRecToMongo(dto);
	}
	
}
