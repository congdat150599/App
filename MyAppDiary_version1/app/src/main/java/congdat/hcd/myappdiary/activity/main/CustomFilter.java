package congdat.hcd.myappdiary.activity.main;

import android.widget.Filter;

import java.util.ArrayList;

import congdat.hcd.myappdiary.model.Note;

public class CustomFilter extends Filter {

    MainAdapter adapter;
    ArrayList<Note> filterList;

    public CustomFilter(ArrayList<Note> filterList,MainAdapter adapter)
    {
        this.adapter=adapter;
        this.filterList=filterList;

    }

    //FILTERING OCURS
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results=new FilterResults();
        //CHECK CONSTRAINT VALIDITY
        if(constraint != null && constraint.length() > 0)
        {
            //CHANGE TO UPPER
            constraint=constraint.toString().toUpperCase();
            //STORE OUR FILTERED PLAYERS
            ArrayList<Note> filteredNotes=new ArrayList<>();

            for (int i=0;i<filterList.size();i++)
            {
                //CHECK
                if(filterList.get(i).getTitle().toUpperCase().contains(constraint))
                {
                    //ADD PLAYER TO FILTERED PLAYERS
                    filteredNotes.add(filterList.get(i));
                }
            }

            results.count=filteredNotes.size();
            results.values=filteredNotes;

        }else
        {
            results.count=filterList.size();
            results.values=filterList;
        }

        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        adapter.notes= (ArrayList<Note>) results.values;

        //REFRESH
        adapter.notifyDataSetChanged();

    }
}