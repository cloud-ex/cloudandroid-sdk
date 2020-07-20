package com.sl.contract.library.data;

import com.chad.library.adapter.base.entity.JSectionEntity;
import com.chad.library.adapter.base.entity.SectionEntity;
import com.contract.sdk.data.ContractTicker;

public class ContractSearchInfo extends JSectionEntity {
    ContractTicker contractTicker;
    boolean isHeader = false;
    public ContractSearchInfo(Boolean isHeader,ContractTicker contractTicker) {
        this.contractTicker = contractTicker;
        this.isHeader = isHeader;
    }


    @Override
    public boolean isHeader() {
        return isHeader;
    }

    public ContractTicker getContractTicker() {
        return contractTicker;
    }

    public void setContractTicker(ContractTicker contractTicker) {
        this.contractTicker = contractTicker;
    }
}
