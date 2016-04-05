package com.zk.base;

import java.util.List;

public class DataUsagePackages extends DataUsageBaseResponse{

    private List<DataUsagePackage> packages;

    public List<DataUsagePackage> getPackages() {
        return packages;
    }

    public void setPackages(List<DataUsagePackage> packages) {
        this.packages = packages;
    }

}
