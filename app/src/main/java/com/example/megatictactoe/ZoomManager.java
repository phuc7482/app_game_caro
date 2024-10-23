package com.example.megatictactoe;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;

public class ZoomManager {

    private static int[] cellSizes;
    private int currentZoomIndex = 5;
    private int m_boardSize;
    private TableLayout m_boardTableLayout;
    private HorizontalScrollView m_horizontalScrollViewer;
    private ScrollView m_verticalScrollViewer;
    private ImageView m_winningLine;

    public ZoomManager(Activity activity, int i, TableLayout tableLayout, ScrollView scrollView, HorizontalScrollView horizontalScrollView, ImageView imageView) {
        this.m_boardTableLayout = tableLayout;
        this.m_verticalScrollViewer = scrollView;
        this.m_horizontalScrollViewer = horizontalScrollView;
        this.m_winningLine = imageView;
        this.m_boardSize = i;
        if (cellSizes == null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            float f = (float) displayMetrics.widthPixels;
            int i2 = (int) (f / ((f / displayMetrics.xdpi) / 0.28f));
            int i3 = (i2 - (i2 / 2)) / 5;
            cellSizes = new int[11];
            for (int i4 = 0; i4 < 11; i4++) {
                cellSizes[i4] = ((5 - i4) * i3) + i2;
            }
        }
        this.currentZoomIndex = 5;
    }

    public void Reset() {
        this.currentZoomIndex = 5;
        ResizeBoard(cellSizes[this.currentZoomIndex]);
    }

    public int GetCurrentCellSize() {
        return cellSizes[this.currentZoomIndex];
    }

    public void ZoomIn() {
        if (this.currentZoomIndex != 0) {
            int scrollX = this.m_horizontalScrollViewer.getScrollX();
            int scrollY = this.m_verticalScrollViewer.getScrollY();
            double d = ((double) cellSizes[this.currentZoomIndex - 1]) / ((double) cellSizes[this.currentZoomIndex]);
            double width = (((double) (scrollX + (this.m_horizontalScrollViewer.getWidth() / 2))) * d) - ((double) (this.m_horizontalScrollViewer.getWidth() / 2));
            double height = (((double) (scrollY + (this.m_verticalScrollViewer.getHeight() / 2))) * d) - ((double) (this.m_verticalScrollViewer.getHeight() / 2));
            this.currentZoomIndex--;
            ResizeBoard(cellSizes[this.currentZoomIndex]);
            ResizeWinningLine(d);
            SetScrollViewerPosition((int) width, (int) height);
        }
    }

    public void ZoomOut() {
        if (this.currentZoomIndex != cellSizes.length - 1) {
            int scrollX = this.m_horizontalScrollViewer.getScrollX();
            int scrollY = this.m_verticalScrollViewer.getScrollY();
            double d = ((double) cellSizes[this.currentZoomIndex + 1]) / ((double) cellSizes[this.currentZoomIndex]);
            double width = (((double) (scrollX + (this.m_horizontalScrollViewer.getWidth() / 2))) * d) - ((double) (this.m_horizontalScrollViewer.getWidth() / 2));
            double height = (((double) (scrollY + (this.m_verticalScrollViewer.getHeight() / 2))) * d) - ((double) (this.m_verticalScrollViewer.getHeight() / 2));
            this.currentZoomIndex++;
            ResizeBoard(cellSizes[this.currentZoomIndex]);
            ResizeWinningLine(d);
            SetScrollViewerPosition((int) width, (int) height);
        }
    }

    public void ResizeBoard(int i) {
        for (int i2 = 0; i2 < this.m_boardSize; i2++) {
            TableRow tableRow = (TableRow) this.m_boardTableLayout.getChildAt(i2);
            for (int i3 = 0; i3 < this.m_boardSize; i3++) {
                tableRow.getChildAt(i3).setLayoutParams(new TableRow.LayoutParams(i, i));
            }
        }
    }

    public void ResizeWinningLine(double d) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.m_winningLine.getLayoutParams();
        layoutParams.width = (int) (((double) layoutParams.width) * d);
        layoutParams.height = (int) (((double) layoutParams.height) * d);
        layoutParams.topMargin = (int) (((double) layoutParams.topMargin) * d);
        layoutParams.leftMargin = (int) (((double) layoutParams.leftMargin) * d);
    }

    public void SetScrollViewerPosition(int i, int i2) {
        this.m_horizontalScrollViewer.scrollTo(i, 0);
        this.m_verticalScrollViewer.scrollTo(0, i2);
    }
}
