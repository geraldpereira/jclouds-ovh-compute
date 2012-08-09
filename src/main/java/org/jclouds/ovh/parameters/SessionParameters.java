package org.jclouds.ovh.parameters;

import static com.google.common.base.Preconditions.checkNotNull;

import org.jclouds.domain.LoginCredentials;
import org.jclouds.javax.annotation.Nullable;

public class SessionParameters {

   private static SessionParameters sessionParameters = null;
   
   public static SessionParameters getSessionParameters() {
      return sessionParameters;
   }
   public static void setSessionParameters(SessionParameters sessionParameters) {
      SessionParameters.sessionParameters = sessionParameters;
   }
   
   public SessionParameters(String login, String pwd, String project, LoginCredentials loginSsh) {
      this.jcloudsLogin = login;
      this.jcloudsPwd = pwd;
      this.jcloudsProj = project;
      this.loginSsh = loginSsh;
   }

   private String jcloudsProj;
   private String jcloudsLogin;
   private String jcloudsPwd;
   private LoginCredentials loginSsh;

   public static final String CLOUD_LANG = "fr";
   public static final boolean CLOUD_MULT = false;

   public LoginCredentials getLoginSsh() {
      return loginSsh;
   }
   public String getJcloudsProj() {
      return jcloudsProj;
   }

   public String getJcloudsLogin() {
      return jcloudsLogin;
   }

   public String getJcloudsPwd() {
      return jcloudsPwd;
   }
   
   public static class Builder{
      private String login = null;
      private String pwd = null;
      private String project = null;
      private LoginCredentials loginSsh = null;
      
      public Builder(){
         
      }
      
      public Builder login(String login){
         checkNotNull(login,"login");
         this.login = login;
         return this;
      }
      
      public Builder pwd(String pwd){
         checkNotNull(pwd,"pwd");
         this.pwd = pwd;
         return this;
      }
      
      public Builder project(String project){
         checkNotNull(project,"project");
         this.project = project;
         return this;
      }
      
      public Builder loginSsh(@Nullable LoginCredentials loginSsh){
         this.loginSsh = loginSsh;
         return this;
      }
      
      public SessionParameters build(){
         return new SessionParameters(login, pwd, project, loginSsh);
      }
   }
}
