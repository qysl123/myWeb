package com.yc.etcp.response.zhongmin;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.yc.etcp.request.zhongmin.OrderXml;

@XmlRootElement(name = "PackageList")
public class OrderOutputXml {
	private final static int SUCCESS_CODE = 0;
	private final static int ERROR_CODE = -1;
	List<Package> packages;

	public List<Package> getPackages() {
		return packages;
	}

	@XmlElement(name ="Package")
	public void setPackages(List<Package> packages) {
		this.packages = packages;
	}
	
	public static OrderOutputXml createSuccessByOrderXml(OrderXml request) {
		OrderOutputXml orderOutputXml = new OrderOutputXml();
		orderOutputXml.setHeader(request, null);
		return orderOutputXml;
	}

	public static OrderOutputXml createFailureByOrderXml(OrderXml request, String msg) {
		OrderOutputXml orderOutputXml = new OrderOutputXml();
		orderOutputXml.setHeader(request, msg);
		return orderOutputXml;
	}
	
	private void setHeader(OrderXml request, String errorMsg) {
		Header header = new Header();
		header.copyRequestHeader(request);
		if (errorMsg == null || errorMsg.isEmpty()) {
			header.setResponseCode(SUCCESS_CODE);
		} else {
			header.setResponseCode(ERROR_CODE);
			header.setResponseInfo(errorMsg);
		}

		Package pack = new Package();
		pack.setHeader(header);
		packages = new ArrayList<Package>();
		packages.add(pack);
	}

}

class Package {
	Header header;

	public Header getHeader() {
		return header;
	}

	@XmlElement(name ="Header")
	public void setHeader(Header header) {
		this.header = header;
	}
}
