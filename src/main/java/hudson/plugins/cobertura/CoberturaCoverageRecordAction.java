package hudson.plugins.cobertura;

import hudson.model.Run;
import hudson.plugins.cobertura.targets.CoverageTarget;
import jenkins.model.RunAction2;

import javax.annotation.CheckForNull;

public class CoberturaCoverageRecordAction implements RunAction2 {
    private transient Run<?, ?> owner;

    private CoverageTarget lastUnhealthyTarget;
    private CoverageTarget lastFailingTarget;

    private CoverageTarget lastUpdatedUnhealthyTarget;
    private CoverageTarget lastUpdatedFailingTarget;


    private boolean autoUpdateHealth;

    private boolean autoUpdateStability;

    @Override
    public void onAttached(Run<?, ?> r) {
        this.owner = r;
    }

    @Override
    public void onLoad(Run<?, ?> r) {
        this.owner = r;
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return null;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return null;
    }

    public CoverageTarget getLastUnhealthyTarget() {
        return lastUnhealthyTarget;
    }

    public void setLastUnhealthyTarget(CoverageTarget lastUnhealthyTarget) {
        this.lastUnhealthyTarget = lastUnhealthyTarget;
    }

    public CoverageTarget getLastFailingTarget() {
        return lastFailingTarget;
    }

    public void setLastFailingTarget(CoverageTarget lastFailingTarget) {
        this.lastFailingTarget = lastFailingTarget;
    }

    public CoverageTarget getLastUpdatedUnhealthyTarget() {
        return lastUpdatedUnhealthyTarget;
    }

    public void setLastUpdatedUnhealthyTarget(CoverageTarget lastUpdatedUnhealthyTarget) {
        this.lastUpdatedUnhealthyTarget = lastUpdatedUnhealthyTarget;
    }

    public CoverageTarget getLastUpdatedFailingTarget() {
        return lastUpdatedFailingTarget;
    }

    public void setLastUpdatedFailingTarget(CoverageTarget lastUpdatedFailingTarget) {
        this.lastUpdatedFailingTarget = lastUpdatedFailingTarget;
    }

    public boolean isAutoUpdateHealth() {
        return autoUpdateHealth;
    }

    public void setAutoUpdateHealth(boolean autoUpdateHealth) {
        this.autoUpdateHealth = autoUpdateHealth;
    }

    public boolean isAutoUpdateStability() {
        return autoUpdateStability;
    }

    public void setAutoUpdateStability(boolean autoUpdateStability) {
        this.autoUpdateStability = autoUpdateStability;
    }
}
