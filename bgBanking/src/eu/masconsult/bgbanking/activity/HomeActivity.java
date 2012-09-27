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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import eu.masconsult.bgbanking.BankingApplication;
import eu.masconsult.bgbanking.R;
import eu.masconsult.bgbanking.activity.fragment.AccountsListFragment;
import eu.masconsult.bgbanking.activity.fragment.ChooseAccountTypeFragment;
import eu.masconsult.bgbanking.banks.Bank;
import eu.masconsult.bgbanking.provider.BankingContract;
import eu.masconsult.bgbanking.sync.SyncAdapter;

public class HomeActivity extends SherlockFragmentActivity {

    private static final String TAG = BankingApplication.TAG + "HomeActivity";

    final BroadcastReceiver syncReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (SyncAdapter.START_SYNC.equals(intent.getAction())) {
                syncStateChanged(true);
            } else if (SyncAdapter.STOP_SYNC.equals(intent.getAction())) {
                syncStateChanged(false);
            }
        }
    };

    private AccountManager accountManager;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);

        accountManager = AccountManager.get(this);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setIcon(R.drawable.ic_icon);
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

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SyncAdapter.START_SYNC);
        intentFilter.addAction(SyncAdapter.STOP_SYNC);
        registerReceiver(syncReceiver, intentFilter);

        syncStateChanged(isSyncActive());
    }

    @Override
    protected void onPause() {
        unregisterReceiver(syncReceiver);

        super.onPause();
    }

    protected void checkForLoggedAccounts() {
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
        addAccountItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS
                | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        addAccountItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                addAccount();
                return true;
            }
        });

        MenuItem sendFeedback = menu.add("Send feedback");
        sendFeedback.setIcon(R.drawable.ic_menu_start_conversation);
        sendFeedback.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        sendFeedback.setOnMenuItemClickListener(new
                MenuItem.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        PackageInfo manager;
                        try {
                            manager = getPackageManager().getPackageInfo(getPackageName(), 0);
                        } catch (NameNotFoundException e) {
                            manager = new PackageInfo();
                            manager.versionCode = 0;
                            manager.versionName = "undef";
                        }
                        try {
                            startActivity(new Intent(Intent.ACTION_SEND)
                                    .setType("plain/text")
                                    .putExtra(Intent.EXTRA_EMAIL, new String[] {
                                            "mobile@masconsult.eu"
                                    })
                                    .putExtra(
                                            Intent.EXTRA_SUBJECT,
                                            "bgBanking v" + manager.versionName + "-"
                                                    + manager.versionCode + " feedback"));
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });

        return true;
    }

    boolean isSyncActive() {
        for (Bank bank : Bank.values()) {
            for (Account account : accountManager.getAccountsByType(bank.getAccountType(this))) {
                if (ContentResolver.isSyncActive(account, BankingContract.AUTHORITY)) {
                    Log.v(TAG, bank + " is syncing");
                    return true;
                }
            }
        }
        Log.v(TAG, "nothing is syncing");
        return false;
    }

    void syncStateChanged(boolean syncActive) {
        Log.v(TAG, "syncStateChanged: " + syncActive);
        setProgressBarIndeterminateVisibility(syncActive);
    }
}
