package com.dls.base.service;

import com.dls.base.controller.form.FormTrainingTestVariantRestController;
import com.dls.base.entity.TestVariantPerson;
import com.dls.base.repository.TestVariantPersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    TestVariantPersonRepository testVariantPersonRepository;

    @Autowired
    FormTrainingTestVariantRestController formTrainingTestVariantRestController;

    @Override
    public void checkTestTimeLimitAll() {
        Set<TestVariantPerson> testVariantPersonSet = testVariantPersonRepository.findAllExpiredInProgress();
        for (TestVariantPerson testVariantPerson : testVariantPersonSet){
            try {
                formTrainingTestVariantRestController.toStatus(testVariantPerson.getId(), "expired");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
