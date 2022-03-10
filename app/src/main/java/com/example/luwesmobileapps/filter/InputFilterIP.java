package com.example.luwesmobileapps.filter;

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilterIP implements InputFilter {

    public InputFilterIP() {

    }

    @Override
    public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
        if (i1 > i) {
            String destTxt = spanned.toString();
            String resultingTxt = destTxt.substring(0, i2)
                    + charSequence.subSequence(i, i1)
                    + destTxt.substring(i3);
            if (!resultingTxt
                    .matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                return "";
            } else {
                String[] splits = resultingTxt.split("\\.");
                for (String split : splits) {
                    if (Integer.parseInt(split) > 255) {
                        return "";
                    }
                }
            }
        }
        return null;
    }
}
