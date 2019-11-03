package congdat.hcd.myappdiary.Api;

import java.util.List;

import congdat.hcd.myappdiary.model.Note;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiInterface {

    @FormUrlEncoded
    @POST("save.php")
    Call<Note> saveNote(
//            @Field("key") String key,
            @Field("title")String title,
            @Field("note") String note,
            @Field("color") int color,
            @Field("date")String date,
            @Field("picture") String picture
    );
    @GET("notes.php")
    Call<List<Note>> getNotes();

    @FormUrlEncoded
    @POST("update.php")
    Call<Note> updateNote(
//            @Field("key") String key,
            @Field("id")int id,
            @Field("title")String title,
            @Field("note") String note,
            @Field("color") int color,
            @Field("date")String date,
            @Field("picture") String picture
    );

    @FormUrlEncoded
    @POST("delete.php")
    Call<Note> deleteNote(
//            @Field("key") String key,
            @Field("id")int id,
            @Field("picture") String picture
    );

}
