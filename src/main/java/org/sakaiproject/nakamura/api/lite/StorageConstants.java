package org.sakaiproject.nakamura.api.lite;

public class StorageConstants {

    /**
     * Property used to select a set of query statements in the finder. These must exist in 
     * the driver configuration and are intended to allow institutions to optimize certain 
     * queries. If not present, a the default set will be used. 
     */
    public static final String CUSTOM_STATEMENT_SET = "_statementset";
    
    /**
     * Property used to set the maximum number of items a query should return per page.
     * The starting row of the query is determined by the page number.
     * Defaults to 25.
     */
    public static final String ITEMS = "_items";
    
    
    /**
     * Page number to start at, defaults to 0.
     */
    public static final String PAGE = "_page";
    
    /**
     * The column on which to perform a sort.
     */
    public static final String SORT = "_sort";

    /**
     * If present Raw Results will be returned as string values for each record.
     */
    public static final String RAWRESULTS = "_rawresults";


}
