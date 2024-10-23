package com.example.megatictactoe;

import androidx.annotation.Nullable;

public class CellIndex {

    private int indexRow;
    private int indexColumn;
    CellIndex(int indexRow,int indexColumn){
        this.indexRow = indexRow;
        this.indexColumn = indexColumn;
    }

    public int getIndexRow() {
        return indexRow;
    }

    public void setIndexRow(int indexRow) {
        this.indexRow = indexRow;
    }

    public int getIndexColumn() {
        return indexColumn;
    }

    public void setIndexColumn(int indexColumn) {
        this.indexColumn = indexColumn;
    }

    @Override
    public boolean equals(@Nullable Object obj) {

        if(obj instanceof CellIndex){
            CellIndex toCompare = (CellIndex) obj;
            if(toCompare.getIndexColumn() == this.indexColumn && toCompare.getIndexRow()==this.indexRow){
                return true;
            }
            else return  false;
        }
        return false;
    }

}
