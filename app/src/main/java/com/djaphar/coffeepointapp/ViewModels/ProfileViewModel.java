package com.djaphar.coffeepointapp.ViewModels;

import android.app.Application;
import android.widget.Toast;

import com.djaphar.coffeepointapp.SupportClasses.ApiClasses.ApiBuilder;
import com.djaphar.coffeepointapp.SupportClasses.ApiClasses.PointsApi;
import com.djaphar.coffeepointapp.SupportClasses.LocalDataClasses.LocalDataDao;
import com.djaphar.coffeepointapp.SupportClasses.LocalDataClasses.LocalDataRoom;
import com.djaphar.coffeepointapp.SupportClasses.LocalDataClasses.Product;
import com.djaphar.coffeepointapp.SupportClasses.LocalDataClasses.User;

import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileViewModel extends AndroidViewModel {

    private LiveData<User> userLiveData;
    private LiveData<List<Product>> userProductsLiveData;
    private LocalDataDao dao;
    private PointsApi pointsApi;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        LocalDataRoom room = LocalDataRoom.getDatabase(application);
        dao = room.localDataDao();
        userLiveData = dao.getUserLiveData();
        userProductsLiveData = dao.getUserProductsLiveData();
        pointsApi = ApiBuilder.getPointsApi();
    }

    public LiveData<User> getUser() {
        return userLiveData;
    }

    public LiveData<List<Product>> getUserProducts() {
        return userProductsLiveData;
    }

    public void requestUpdateUser(User user, HashMap<String, String> headersMap)  {
        Call<User> call = pointsApi.updateUser(user.get_id(), headersMap, user);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getApplication(), response.message(), Toast.LENGTH_SHORT).show();
                    return;
                }
                User updatedUser = response.body();
                if (updatedUser == null) {
                    return;
                }
                Integer updatedHash = updatedUser.determineHash();
                updatedUser.setUserHash(updatedHash);
                LocalDataRoom.databaseWriteExecutor.execute(() -> dao.updateUser(updatedUser));
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Toast.makeText(getApplication(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void requestAddProduct(Product product, HashMap<String, String> headersMap) {
        Call<Product> call = pointsApi.requestAddProduct(headersMap, product);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(@NonNull Call<Product> call, @NonNull Response<Product> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getApplication(), response.message(), Toast.LENGTH_SHORT).show();
                    return;
                }
                LocalDataRoom.databaseWriteExecutor.execute(() -> dao.setUserProduct(response.body()));
            }

            @Override
            public void onFailure(@NonNull Call<Product> call, @NonNull Throwable t) {
                Toast.makeText(getApplication(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void requestDeleteProduct(String id, HashMap<String, String> headersMap) {
        Call<Product> call = pointsApi.requestDeleteProduct(id, headersMap);
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(@NonNull Call<Product> call, @NonNull Response<Product> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(getApplication(), response.message(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (response.body() == null) {
                    return;
                }
                LocalDataRoom.databaseWriteExecutor.execute(() -> dao.deleteUserProduct(response.body().get_id()));
            }

            @Override
            public void onFailure(@NonNull Call<Product> call, @NonNull Throwable t) {
                Toast.makeText(getApplication(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}