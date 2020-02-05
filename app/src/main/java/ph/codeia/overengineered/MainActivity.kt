package ph.codeia.overengineered

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelStoreOwner
import dagger.BindsInstance
import dagger.Component
import ph.codeia.shiv.Shiv
import shiv.FragmentBindings
import shiv.SharedViewModelProviders


class MainActivity : AppCompatActivity(R.layout.activity_main) {
	override fun onCreate(savedInstanceState: Bundle?) {
		DaggerFragmentComponent.factory()
			.create(this)
			.fragmentFactory
			.let { supportFragmentManager.fragmentFactory = it }
		super.onCreate(savedInstanceState)
	}
}

@Component(modules = [
	Shiv::class,
	SharedViewModelProviders::class,
	FragmentBindings::class
])
interface FragmentComponent {
	val fragmentFactory: FragmentFactory
	val loginComponentFactory: LoginComponent.Factory

	@Component.Factory
	interface Factory {
		fun create(@BindsInstance owner: ViewModelStoreOwner): FragmentComponent
	}
}
