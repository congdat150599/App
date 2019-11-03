package congdat.hcd.myappdiary.activity.main;

import java.util.List;
import congdat.hcd.myappdiary.model.Note;

public interface MainView{
    void showloading();
    void hideloading();
    void onGetResult(List<Note> notes);
    void onErrorloading(String message);
}
