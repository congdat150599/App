package congdat.hcd.myappdiary.activity.main;

import androidx.annotation.NonNull;

import java.util.List;

import congdat.hcd.myappdiary.Api.ApiClient;
import congdat.hcd.myappdiary.Api.ApiInterface;
import congdat.hcd.myappdiary.model.Note;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainPresenter {
    private MainView view;

    public MainPresenter(MainView view) {
        this.view = view;

    }
    void getData(){
        view.showloading();

        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<List<Note>> call = apiInterface.getNotes();
        call.enqueue(new Callback<List<Note>>() {
            @Override
            public void onResponse(@NonNull Call<List<Note>> call,@NonNull Response<List<Note>> response) {
                view.hideloading();
                if (response.isSuccessful() && response.body() != null){
                    view.onGetResult(response.body());
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Note>> call, @NonNull Throwable t) {
                view.hideloading();
                view.onErrorloading(t.getLocalizedMessage());

            }
        });
    }
}
