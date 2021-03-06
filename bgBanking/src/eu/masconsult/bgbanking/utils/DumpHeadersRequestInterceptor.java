/*******************************************************************************
 * Copyright (c) 2012 MASConsult Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package eu.masconsult.bgbanking.utils;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;

import android.util.Log;
import eu.masconsult.bgbanking.BankingApplication;

public class DumpHeadersRequestInterceptor implements HttpRequestInterceptor {

    private static final String TAG = BankingApplication.TAG + "DumpHeadersRI";

    @Override
    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        for (Header header : request.getAllHeaders()) {
            Log.v(TAG, "> " + header);
        }
    }

}
