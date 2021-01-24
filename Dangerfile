ktlint_report_dir = "**/ktlintMainCheck.xml"
Dir[ktlint_report_dir].each do |file_name|
  checkstyle_format.base_path = Dir.pwd
  checkstyle_format.report file_name
end
ktlint_test_report_dir = "**/ktlintTestCheck.xml"
Dir[ktlint_report_dir].each do |file_name|
  checkstyle_format.base_path = Dir.pwd
  checkstyle_format.report file_name
end

junit_output_dir = "**/test-results/**/*.xml"
Dir[junit_output_dir].each do |file_name|
  junit.parse(file_name)
  junit.show_skipped_tests = true
  junit.report
end
