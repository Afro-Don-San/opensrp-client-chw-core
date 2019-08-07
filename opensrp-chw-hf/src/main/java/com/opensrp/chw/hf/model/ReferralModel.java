package com.opensrp.chw.hf.model;

import com.opensrp.chw.core.model.BaseReferralModel;
import com.opensrp.chw.core.utils.CoreConstants;

import org.smartregister.cursoradapter.SmartRegisterQueryBuilder;
import org.smartregister.family.util.DBConstants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ReferralModel extends BaseReferralModel {

    @Override
    public String mainSelect(String tableName, String mainCondition) {
        SmartRegisterQueryBuilder queryBuilder = new SmartRegisterQueryBuilder();
        queryBuilder.selectInitiateMainTable(tableName, mainColumns(tableName), "_id");
        queryBuilder.customJoin("INNER JOIN " + CoreConstants.TABLE_NAME.CHILD + " ON  " + tableName + "." + CoreConstants.DB_CONSTANTS.FOR + " = " + CoreConstants.TABLE_NAME.TASK + "." + CoreConstants.DB_CONSTANTS.FOR + " COLLATE NOCASE ");

        return queryBuilder.mainCondition(mainCondition);
    }

    @Override
    protected String[] mainColumns(String tableName) {
        Set<String> columns = new HashSet<>(Arrays.asList(super.mainColumns(tableName)));
        addClientDetails(CoreConstants.TABLE_NAME.CHILD, columns);
        return columns.toArray(new String[]{});
    }

    private void addClientDetails(String table, Set<String> columns) {
        columns.add(table + "." + DBConstants.KEY.FIRST_NAME);
        columns.add(table + "." + DBConstants.KEY.MIDDLE_NAME);
        columns.add(table + "." + DBConstants.KEY.LAST_NAME);
        columns.add(table + "." + DBConstants.KEY.DOB);
        columns.add(table + "." + DBConstants.KEY.GENDER);

    }
}
