package com.sencha.gxt.demo.client.application.simplegrid.widgets;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfigBean;
import com.sencha.gxt.data.shared.loader.LoadResultListStoreBinding;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.grid.GridView;

public class SimpleGridViewWidget implements IsWidget {

  private Grid<Data> grid;
  private GridView<Data> gridView;
  private PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Data>> pagingLoader;
  
  private int columnsSize;
  private int rowsSize;

  public void setColRowSize(int columnsSize, int rowsSize) {
    this.columnsSize = columnsSize;
    this.rowsSize = rowsSize;
  }
  
  public void updateColRowSize(int columnsSize, int rowsSize) {
    setColRowSize(columnsSize, rowsSize);
    
    ColumnModel<Data> cm = new ColumnModel<Data>(getColumns());
    
    ListStore<Data> store = grid.getStore();
    store.clear();
    
    grid.reconfigure(store, cm);
    
    pagingLoader.load();
  }
  
  @Override
  public Widget asWidget() {
    if (grid == null) {
      grid = createGrid();
    }
    return grid;
  }
  
  private int getColumnsSize() {
    return columnsSize;
  }
  
  private int getRowsSize() {
    return rowsSize;
  }

  public Grid<Data> createGrid() {
    ListStore<Data> listStore = getStore();
    pagingLoader = getLoader(listStore);

    ColumnModel<Data> cm = new ColumnModel<Data>(getColumns());

    gridView = new GridView<Data>();

    grid = new Grid<Data>(listStore, cm, gridView) {
      @Override
      protected void onAfterFirstAttach() {
        super.onAfterFirstAttach();
        pagingLoader.load();
      }
    };
    grid.setLoader(pagingLoader);
    grid.setLoadMask(true);

    return grid;
  }

  private List<ColumnConfig<Data, ?>> getColumns() {
    List<ColumnConfig<Data, ?>> columns = new ArrayList<ColumnConfig<Data, ?>>();

    for (int c = 0; c < getColumnsSize(); c++) {
      String header = "col" + c;
      ColumnConfig<Data, String> col = new ColumnConfig<Data, String>(new ValueProviderExt(c), 75, header);
      col.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
      col.setCellPadding(false);

      columns.add(col);
    }
    return columns;
  }

  private PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Data>> getLoader(ListStore<Data> store) {
    RpcProxy<FilterPagingLoadConfig, PagingLoadResult<Data>> proxy = new RpcProxy<FilterPagingLoadConfig, PagingLoadResult<Data>>() {
      @Override
      public void load(FilterPagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<Data>> callback) {
        // Simulate an RPC request to get the data
        getDatas(loadConfig, callback);
      }
    };

    PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Data>> loader = new PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Data>>(
        proxy);
    loader.useLoadConfig(new FilterPagingLoadConfigBean());
    loader.addLoadHandler(new LoadResultListStoreBinding<FilterPagingLoadConfig, Data, PagingLoadResult<Data>>(store));
    loader.setRemoteSort(true);

    return loader;
  }

  private ListStore<Data> getStore() {
    ListStore<Data> store = new ListStore<Data>(new ModelKeyProvider<Data>() {
      @Override
      public String getKey(Data item) {
        return item.getKey();
      }
    });
    return store;
  }

  private void getDatas(FilterPagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<Data>> callback) {
    final int offset = loadConfig.getOffset();
    int limit = loadConfig.getLimit();

    final List<Data> datas = new ArrayList<Data>();
    for (int i = offset; i < offset + limit; i++) {
      datas.add(getData(i));
    }

    PagingLoadResult<Data> result = new PagingLoadResult<Data>() {
      @Override
      public List<Data> getData() {
        return datas;
      }

      @Override
      public void setTotalLength(int totalLength) {
      }

      @Override
      public void setOffset(int offset) {
      }

      @Override
      public int getTotalLength() {
        return getRowsSize();
      }

      @Override
      public int getOffset() {
        return offset;
      }
    };
    callback.onSuccess(result);
  }

  private Data getData(int row) {
    String key = "key" + row;

    String[] values = new String[getColumnsSize()];
    for (int col = 0; col < getColumnsSize(); col++) {
      values[col] = "" + col + "," + row;
    }

    Data data = new Data(key, values);
    return data;
  }

  public class ValueProviderExt implements ValueProvider<Data, String> {
    private int index;

    public ValueProviderExt(int index) {
      this.index = index;
    }

    @Override
    public String getValue(Data data) {
      return data.getValue(index);
    }

    @Override
    public void setValue(Data object, String value) {
    }

    @Override
    public String getPath() {
      return "path" + index;
    }
  }

  public class Data {
    private String key;
    private String[] values;

    public Data(String key, String[] values) {
      this.key = key;
      this.values = values;
    }

    public String getKey() {
      return key;
    }

    public void setKey(String key) {
      this.key = key;
    }

    public String getValue(int index) {
      return values[index];
    }

    public void setValue(int index, String value) {
      this.values[index] = value;
    }

    @Override
    public String toString() {
      String s = "Data(";
      s += "key=" + key;
      s += ")";
      return s;
    }
  }

  public PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Data>> getPaingLoader() {
    return pagingLoader;
  }

  public void refresh() {
    grid.getView().refresh(true);
  }
}