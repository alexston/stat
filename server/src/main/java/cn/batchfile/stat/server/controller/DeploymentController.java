package cn.batchfile.stat.server.controller;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import cn.batchfile.stat.domain.Deployment;
import cn.batchfile.stat.server.service.DeploymentService;

@RestController
public class DeploymentController {

	protected static final Logger log = LoggerFactory.getLogger(DeploymentController.class);

	@Autowired
	private DeploymentService deploymentService;

	@GetMapping("/api/v2/deployment")
	public ResponseEntity<List<Deployment>> getDeployments(WebRequest request,
			@RequestParam(value = "node", required = false) String node) throws IOException {

		long lastModified = deploymentService.getLastModified();
		if (lastModified < 0) {
			return new ResponseEntity<List<Deployment>>(HttpStatus.NOT_FOUND);
		}

		if (request.checkNotModified(lastModified)) {
			return new ResponseEntity<List<Deployment>>(HttpStatus.NOT_MODIFIED);
		}

		List<Deployment> deployments = deploymentService.getDeployments();
		if (StringUtils.isNotEmpty(node)) {
			deployments = deployments.stream()
					.filter(d -> StringUtils.equals(d.getNode(), node))
					.collect(Collectors.toList());
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setLastModified(lastModified);
		headers.setCacheControl("no-cache");
		return new ResponseEntity<List<Deployment>>(deployments, headers, HttpStatus.OK);
	}

	@GetMapping("/api/v2/service/{name}/deployment")
	public ResponseEntity<List<Deployment>> getDeploymentsOfService(WebRequest request, @PathVariable("name") String name) throws IOException {

		List<Deployment> deployments = deploymentService.getDeployments(name);
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");
		return new ResponseEntity<List<Deployment>>(deployments, headers, HttpStatus.OK);
	}

	@GetMapping("/api/v2/service/{name}/deployment/count")
	public ResponseEntity<Integer> getDeploymentsCountOfService(WebRequest request, @PathVariable("name") String name) throws IOException {

		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");

		List<Deployment> deployments = deploymentService.getDeployments(name);
		int count = deployments == null ? 0 : deployments.size();

		return new ResponseEntity<Integer>(count, headers, HttpStatus.OK);
	}

	@GetMapping("/api/v2/node/{id}/deployment")
	public ResponseEntity<List<Deployment>> getDeploymentsOfNode(WebRequest request, @PathVariable("id") String nodeId) throws IOException {

		List<Deployment> deployments = deploymentService.getDeployments();
		deployments = deployments.stream()
				.filter(d -> StringUtils.equals(d.getNode(), nodeId))
				.collect(Collectors.toList());

		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");
		return new ResponseEntity<List<Deployment>>(deployments, headers, HttpStatus.OK);
	}

	@GetMapping("/api/v2/node/{id}/deployment/count")
	public ResponseEntity<Integer> getDeploymentsCountOfNode(WebRequest request, @PathVariable("id") String nodeId) throws IOException {

		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl("no-cache");

		List<Deployment> deployments = deploymentService.getDeployments();
		deployments = deployments.stream()
				.filter(d -> StringUtils.equals(d.getNode(), nodeId))
				.collect(Collectors.toList());
		int count = deployments == null ? 0 : deployments.size();

		return new ResponseEntity<Integer>(count, headers, HttpStatus.OK);
	}

}
