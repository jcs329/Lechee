package org.linphone.setup;
/*
GenericLoginFragment.java
Copyright (C) 2012  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
import org.linphone.LinphonePreferences;
import org.linphone.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;
/**
 * @author Sylvain Berfini
 */
public class GenericLoginFragment extends Fragment implements OnClickListener {
	private EditText login, password, rms_address;
	private Switch switcher;
	private ImageView apply;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.setup_generic_login, container, false);
		
		login = (EditText) view.findViewById(R.id.setup_username);
		password = (EditText) view.findViewById(R.id.setup_password);
		//domain = (EditText) view.findViewById(R.id.setup_domain);
		rms_address = (EditText) view.findViewById(R.id.setup_rms_address);
		/*Neil add, 2015/01/06*/
		switcher=(Switch)view.findViewById(R.id.setup_switcher);
		/*Neil add, 2015/01/06*/
		apply = (ImageView) view.findViewById(R.id.setup_apply);
		apply.setOnClickListener(this);
		
		//domain.setText("quantatw.com"); // LiangBin add, 20141218
		/*Neil ADD,2015/01/16*/
		LinphonePreferences mPrefs = LinphonePreferences.instance();
		login.setText(mPrefs.getRmsid());
		password.setText(mPrefs.getRmspw());
		switcher.setChecked(mPrefs.getRmsflag());
		rms_address.setText(mPrefs.getRmsAddress());
		/*Neil ADD,2015/01/16*/
		return view;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		if (id == R.id.setup_apply) {
			if (login.getText() == null || login.length() == 0 || password.getText() == null || password.length() == 0 || rms_address.getText() == null || rms_address.length() == 0) {
				Toast.makeText(getActivity(), getString(R.string.first_launch_no_login_password), Toast.LENGTH_LONG).show();
				return;
			}
			/*Neil modified, 2015/01/06*/
			//SetupActivity.instance().genericLogIn(login.getText().toString(), password.getText().toString(), domain.getText().toString());
			SetupActivity.instance().Q_genericLogIn(login.getText().toString(), password.getText().toString(), rms_address.getText().toString(),switcher.isChecked());
			/*Neil modified, 2015/01/06*/
		}
	}
}
