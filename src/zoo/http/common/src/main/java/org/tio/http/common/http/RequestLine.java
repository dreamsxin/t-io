package org.tio.http.common.http;

/**
 * @author tanyaowu 
 * 2017年6月28日 下午2:20:32
 */
public class RequestLine {
	private String method;
	private String path;
	private String queryStr; //譬如http://www.163.com?name=tan&id=789，那些此值就是name=tan&id=789
	private String version;
	private String initStr;

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @param method the method to set
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the initStr
	 */
	public String getInitStr() {
		return initStr;
	}

	/**
	 * @param initStr the initStr to set
	 */
	public void setInitStr(String initStr) {
		this.initStr = initStr;
	}

	/**
	 * @return the queryStr
	 */
	public String getQueryStr() {
		return queryStr;
	}

	/**
	 * @param queryStr the queryStr to set
	 */
	public void setQueryStr(String queryStr) {
		this.queryStr = queryStr;
	}
}
