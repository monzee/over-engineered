package ph.codeia.overengineered

import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.savedstate.SavedStateRegistryOwner
import dagger.*
import ph.codeia.overengineered.mvi.login.LoginView as MviLoginView
import ph.codeia.shiv.Shiv
import shiv.FragmentBindings


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
	FragmentBindings::class,
	ActivityServices::class
])
interface FragmentComponent {
	val fragmentFactory: FragmentFactory
	val loginViewBinder: LoginView.Binder
	val mviLoginViewBinder: MviLoginView.Binder

	@Component.Factory
	interface Factory {
		fun create(@BindsInstance parent: AppCompatActivity): FragmentComponent
	}
}


@Module(includes = [ActivityServices.Providers::class])
interface ActivityServices {
	@Binds
	fun stateOwner(activity: AppCompatActivity): SavedStateRegistryOwner

	@Module
	object Providers {
		@[JvmStatic Provides]
		fun app(activity: AppCompatActivity): Application = run {
			activity.application
		}

		@[JvmStatic Provides]
		fun undeadStateVmFactory(
			app: Application,
			stateOwner: SavedStateRegistryOwner
		): SavedStateViewModelFactory = run {
			SavedStateViewModelFactory(app, stateOwner)
		}
	}
}
