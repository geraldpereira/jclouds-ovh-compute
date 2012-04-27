package org.jclouds.ovh.parameters;

import org.jclouds.domain.LoginCredentials;

public class SessionParameters {

	public SessionParameters(){
		
	}
	
	private static String jcloudsProj  = null;
	private static String jcloudsLogin = null;
	private static String jcloudsPwd   = null;
	public static LoginCredentials loginSsh = null;
	
	
	public static final String CLOUD_LANG  = "fr";
	public static final boolean CLOUD_MULT = false;
	
	private static final String CLOUD_NEW_INSTANCE= "jclouds";
	private static			int    jcloudsId	= 0;
	
	public static String getUniqueJCloudsInstanceName(){return CLOUD_NEW_INSTANCE+(jcloudsId++);}
	
	
	
	public static String getJcloudsProj() {
		return jcloudsProj;
	}

	public static void setJcloudsProj(String jcloudsProj) {
		SessionParameters.jcloudsProj = jcloudsProj;
	}

	public static String getJcloudsLogin() {
		return jcloudsLogin;
	}

	public static void setJcloudsLogin(String jcloudsLogin) {
		SessionParameters.jcloudsLogin = jcloudsLogin;
	}

	public static String getJcloudsPwd() {
		return jcloudsPwd;
	}

	public static void setJcloudsPwd(String jcloudsPwd) {
		SessionParameters.jcloudsPwd = jcloudsPwd;
	}

	public static int getJcloudsId() {
		return jcloudsId;
	}

	public static void setJcloudsId(int jcloudsId) {
		SessionParameters.jcloudsId = jcloudsId;
	}

}
