/*
 * Copyright 2021 Harness Inc.
 * 
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.harness.licensing.remote;

import io.harness.licensing.beans.response.CheckExpiryResultDTO;
import io.harness.licensing.beans.summary.LicensesWithSummaryDTO;
import io.harness.ng.core.dto.ResponseDTO;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NgLicenseHttpClient {
  String CHECK_NG_LICENSE_EXPIRY_API = "licenses/{accountId}/check-expiry";
  String SOFT_DELETE_API = "licenses/{accountId}/soft-delete";
  String LICENSE_SUMMARY_API = "licenses/{accountId}/summary";

  @GET(CHECK_NG_LICENSE_EXPIRY_API)
  Call<ResponseDTO<CheckExpiryResultDTO>> checkExpiry(@Path("accountId") String accountId);

  @GET(SOFT_DELETE_API) Call<ResponseDTO<Boolean>> softDelete(@Path("accountId") String accountId);

  @GET(LICENSE_SUMMARY_API)
  Call<ResponseDTO<LicensesWithSummaryDTO>> getLicenseSummary(
      @Path("accountId") String accountId, @Query("moduleType") String moduleType);
}
