/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package com.parse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import bolts.Task;

/** package */ class ParseRESTUserCommand extends ParseRESTCommand {

  private static final String HEADER_REVOCABLE_SESSION = "X-Parse-Revocable-Session";
  private static final String HEADER_TRUE = "1";

  public static ParseRESTUserCommand getCurrentUserCommand(String sessionToken) {
    return new ParseRESTUserCommand("users/me", ParseHttpRequest.Method.GET, null, sessionToken);
  }

  //region Authentication

  public static ParseRESTUserCommand signUpUserCommand(JSONObject parameters, String sessionToken,
      boolean revocableSession) {
    return new ParseRESTUserCommand(
        "classes/_User", ParseHttpRequest.Method.POST, parameters, sessionToken, revocableSession);
  }

  public static ParseRESTUserCommand logInUserCommand(String username, String password,
      boolean revocableSession) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("username", username);
    parameters.put("password", password);
    return new ParseRESTUserCommand(
        "login", ParseHttpRequest.Method.GET, parameters, null, revocableSession);
  }

  public static ParseRESTUserCommand serviceLogInUserCommand(
      String authType, Map<String, String> authData, boolean revocableSession) {

    // Mimic ParseSetOperation
    JSONObject parameters;
    try {
      JSONObject authenticationData = new JSONObject();
      authenticationData.put(authType, PointerEncoder.get().encode(authData));

      parameters = new JSONObject();
      parameters.put("authData", authenticationData);
    } catch (JSONException e) {
      throw new RuntimeException("could not serialize object to JSON");
    }

    return serviceLogInUserCommand(parameters, null, revocableSession);
  }

  public static ParseRESTUserCommand serviceLogInUserCommand(JSONObject parameters,
      String sessionToken, boolean revocableSession) {
    return new ParseRESTUserCommand(
        "users", ParseHttpRequest.Method.POST, parameters, sessionToken, revocableSession);
  }

  //endregion

  public static ParseRESTUserCommand resetPasswordResetCommand(String email) {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("email", email);
    return new ParseRESTUserCommand(
        "requestPasswordReset", ParseHttpRequest.Method.POST, parameters, null);
  }

  private boolean isRevocableSessionEnabled;
  private int statusCode;

  private ParseRESTUserCommand(
      String httpPath,
      ParseHttpRequest.Method httpMethod,
      Map<String, ?> parameters,
      String sessionToken) {
    this(httpPath, httpMethod, parameters, sessionToken, false);
  }

  private ParseRESTUserCommand(
      String httpPath,
      ParseHttpRequest.Method httpMethod,
      Map<String, ?> parameters,
      String sessionToken, boolean isRevocableSessionEnabled) {
    super(httpPath, httpMethod, parameters, sessionToken);
    this.isRevocableSessionEnabled = isRevocableSessionEnabled;
  }

  private ParseRESTUserCommand(
      String httpPath,
      ParseHttpRequest.Method httpMethod,
      JSONObject parameters,
      String sessionToken, boolean isRevocableSessionEnabled) {
    super(httpPath, httpMethod, parameters, sessionToken);
    this.isRevocableSessionEnabled = isRevocableSessionEnabled;
  }

  public int getStatusCode() {
    return statusCode;
  }

  @Override
  protected void addAdditionalHeaders(ParseHttpRequest.Builder requestBuilder) {
    super.addAdditionalHeaders(requestBuilder);
    if (isRevocableSessionEnabled) {
      requestBuilder.addHeader(HEADER_REVOCABLE_SESSION, HEADER_TRUE);
    }
  }

  @Override
  protected Task<JSONObject> onResponseAsync(ParseHttpResponse response,
      ProgressCallback progressCallback) {
    statusCode = response.getStatusCode();
    return super.onResponseAsync(response, progressCallback);
  }

}
