// to run locally use:
//      spring run greeting.groovy
//
// to deploy to Cloud Foundry use:
//      spring jar greeting.jar greeting.groovy
//      cf push greeting -p greeting.jar

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class GreetingRestController {
	@RequestMapping("/greet/{name}")
	def hi(@PathVariable String name) {
		[greeting: "Hello, " + name + "!"]
	}
}