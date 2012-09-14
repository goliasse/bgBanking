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

package eu.masconsult.bgbanking.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuItem;

import com.zubhium.ZubhiumSDK;

import eu.masconsult.bgbanking.BankingApplication;
import eu.masconsult.bgbanking.R;
import eu.masconsult.bgbanking.activity.fragment.AccountsListFragment;
import eu.masconsult.bgbanking.activity.fragment.ChooseAccountTypeFragment;
import eu.masconsult.bgbanking.banks.Bank;

public class HomeActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        enableZubhiumUpdates(this);
    }

    @Override
    protected void onDestroy() {
        disableZubhiumUpdates(this);
        super.onDestroy();
    }

    // TODO: extract to some utility class
    private static ZubhiumSDK getZubhiumSDK(Context context) {
        BankingApplication globalContext = (BankingApplication) context.getApplicationContext();
        return globalContext != null ? globalContext.getZubhiumSDK() : null;
    }

    // TODO: extract to some utility class
    private static void enableZubhiumUpdates(Activity activity) {
        ZubhiumSDK sdk = getZubhiumSDK(activity);
        if (sdk != null) {
            /**
             * Lets register kill switch / update receiver Read more :
             * https://www.zubhium.com/docs/sendmessage/
             */
            sdk.registerUpdateReceiver(activity);
        }
    }

    // TODO: extract to some utility class
    private static void disableZubhiumUpdates(Activity activity) {
        ZubhiumSDK sdk = getZubhiumSDK(activity);
        if (sdk != null) {
            sdk.unRegisterUpdateReceiver();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkForLoggedAccounts();

        FragmentManager fm = getSupportFragmentManager();

        // Create the list fragment and add it as our sole content.
        if (fm.findFragmentById(android.R.id.content) == null) {
            AccountsListFragment list = new AccountsListFragment();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        }
    }

    protected void checkForLoggedAccounts() {
        AccountManager accountManager = AccountManager.get(this);
        Bank[] banks = Bank.values();
        String[] accountTypes = new String[banks.length];

        boolean hasAccounts = false;
        for (int i = 0; i < banks.length; i++) {
            accountTypes[i] = banks[i].getAccountType(this);
            if (accountManager.getAccountsByType(banks[i].getAccountType(this)).length > 0) {
                hasAccounts = true;
            }
        }

        if (!hasAccounts) {
            addAccount();
        }
    }

    void addAccount() {
        ChooseAccountTypeFragment accountTypesFragment = new ChooseAccountTypeFragment();
        accountTypesFragment.show(getSupportFragmentManager(), "AccountsDialog");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem addAccountItem = menu.add("Add account");
        addAccountItem.setIcon(R.drawable.ic_menu_add);
        MenuItemCompat.setShowAsAction(addAccountItem, MenuItemCompat.SHOW_AS_ACTION_NEVER);
        addAccountItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                addAccount();
                return true;
            }
        });

        return true;
    }
}
