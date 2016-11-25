package com.quantatw.sls.cloudapi;

import com.quantatw.sls.pack.account.AccountReqPack;
import com.quantatw.sls.pack.account.AccountResPack;

import android.util.Log;


public class TestUserRegister extends TestCloudApi<AccountReqPack, AccountResPack> {

	@Override
	String Request(AccountReqPack reqPack) {
		// TODO Auto-generated method stub
		
		String data = gson.toJson(reqPack);
		Log.d("CloudAPI", data);
		return PostBaseReq(API_USERREGISTER, data);
	}



}
