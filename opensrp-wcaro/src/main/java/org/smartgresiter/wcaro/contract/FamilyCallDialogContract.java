package org.smartgresiter.wcaro.contract;

public interface FamilyCallDialogContract {

    interface Presenter {

        void updateHeadOfFamily(Model model);

        void updateCareGiver(Model model);

        void initalize();

    }

    interface View {

        void refreshHeadOfFamilyView(Model model);

        void refreshCareGiverView(Model model);

        Dialer getPendingCallRequest();

        void setPendingCallRequest(Dialer dialer);

        Presenter initializePresenter();

    }

    interface Interactor {

        void getHeadOfFamily(Presenter presenter);

        void getCareGiver(Presenter presenter);

    }

    interface Dialer {
        void callMe();
    }

    interface Model {

        String getName();

        String getRole();

        String getPhoneNumber();

        void setName(String name);

        void setRole(String role);

        void setPhoneNumber(String phoneNumber);
    }
}
